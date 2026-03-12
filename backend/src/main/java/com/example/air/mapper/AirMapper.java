package com.example.air.mapper;

import com.example.air.entity.AirRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AirMapper extends BaseMapper<AirRecord> {

    // 【修改】红榜：先按城市分组取最大ID(即最新一条)，再排序
    @Select("SELECT * FROM air_record " +
            "WHERE id IN (SELECT MAX(id) FROM air_record GROUP BY city_name) " +
            "ORDER BY aqi DESC LIMIT 5")
    List<AirRecord> selectTop5Worst();

    // 【修改】黑榜：同理，只看最新数据
    @Select("SELECT * FROM air_record " +
            "WHERE id IN (SELECT MAX(id) FROM air_record GROUP BY city_name) " +
            "ORDER BY aqi ASC LIMIT 5")
    List<AirRecord> selectTop5Best();

    // 历史趋势查询保持不变
    @Select("SELECT * FROM air_record WHERE city_name = #{cityName} ORDER BY record_time ASC")
    List<AirRecord> selectHistoryByCity(String cityName);
}