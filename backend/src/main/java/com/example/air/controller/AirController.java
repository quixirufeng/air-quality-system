package com.example.air.controller;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import com.example.air.common.Result;
import java.io.IOException;
import java.net.URLEncoder;
import com.example.air.entity.AirRecord;
import com.example.air.mapper.AirMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/air")
@CrossOrigin(origins = "*") // 核心：允许前端跨域访问
public class AirController {

    @Autowired
    private AirMapper airMapper;

    // 前端 App.vue 请求的就是这个地址: /api/air/list
    @GetMapping("/list")
    public List<AirRecord> getAll() {
        // 使用 MyBatis-Plus 直接查询所有数据
        return airMapper.selectList(null);
    }

    // 添加这个新接口
    @GetMapping("/rank")
    public Result getRankData() {
        Map<String, List<AirRecord>> map = new HashMap<>();
        map.put("worst", airMapper.selectTop5Worst()); // 污染最重
        map.put("best", airMapper.selectTop5Best());   // 空气最好
        return Result.success(map);
    }

    @GetMapping("/history")
    public Result getHistory(@RequestParam String cityName) {
        List<AirRecord> list = airMapper.selectHistoryByCity(cityName);
        return Result.success(list);
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) List<String> cityNames, // 1. 改成 List，支持多选
                       @RequestParam(required = false) String startDate,       // 2. 开始日期 (yyyy-MM-dd)
                       @RequestParam(required = false) String endDate) throws IOException { // 3. 结束日期

        QueryWrapper<AirRecord> queryWrapper = new QueryWrapper<>();

        // 1. 城市多选筛选
        if (cityNames != null && !cityNames.isEmpty()) {
            queryWrapper.in("city_name", cityNames); // SQL: WHERE city_name IN ('北京', '上海'...)
        }

        // 2. 时间范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            // 大于等于当天的 00:00:00
            queryWrapper.ge("record_time", startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            // 小于等于当天的 23:59:59
            queryWrapper.le("record_time", endDate + " 23:59:59");
        }

        queryWrapper.orderByDesc("record_time");

        List<AirRecord> list = airMapper.selectList(queryWrapper);

        // --- 下面导出 Excel 的逻辑保持不变 ---
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("id", "编号");
        writer.addHeaderAlias("cityName", "城市名称");
        writer.addHeaderAlias("aqi", "AQI指数");
        writer.addHeaderAlias("pm25", "PM2.5浓度");
        writer.addHeaderAlias("level", "污染等级");
        writer.addHeaderAlias("recordTime", "监测时间");

        writer.write(list, true);
        writer.setColumnWidth(5, 30);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("空气质量数据报表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }
}