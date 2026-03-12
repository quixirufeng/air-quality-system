package com.example.air.task;

import com.example.air.entity.AirRecord;
import com.example.air.mapper.AirMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class AirTask {

    @Autowired
    private AirMapper airMapper;

    private final Random random = new Random();

    // fixedRate = 3600000
    // cron 表达式 cron = "0 0 0 * * ?"
    @Scheduled(cron = "0 0 * * * ?")
    public void generateSensorData() {
        System.out.println("【数据引擎】开始生成新一轮实时监测数据...");

        // 1. 获取当前所有城市的最新那条数据作为“基准”
        // (这里为了简单，我们先查出所有去重后的城市名，实际可以直接硬编码或查表)
        // 假设我们只针对数据库里已有的那 34 个城市更新
        List<AirRecord> lastRecords = airMapper.selectList(null);
        // 注意：上面这个 selectList 会查出所有历史，量大时要改 SQL。
        // 优化方案：应该写个 SQL select * from air_record group by city_name having max(record_time)
        // 但作为演示，我们可以偷懒：只更新数据库里存在的城市

        // 我们用更简单的逻辑：只更新几个主要城市演示动态效果，或者遍历城市列表
        String[] cities = {
                "北京市", "上海市", "天津市", "重庆市", "河北省", "山西省", "辽宁省", "吉林省",
                "黑龙江省", "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省",
                "河南省", "湖北省", "湖南省", "广东省", "海南省", "四川省", "贵州省",
                "云南省", "陕西省", "甘肃省", "青海省", "台湾省", "内蒙古自治区",
                "广西壮族自治区", "西藏自治区", "宁夏回族自治区", "新疆维吾尔自治区",
                "香港特别行政区", "澳门特别行政区", "南海诸岛"
        };

        for (String city : cities) {
            // 2. 查找该城市最近一次的记录，为了拿到基准值
            List<AirRecord> history = airMapper.selectHistoryByCity(city);

            int lastAqi = 50; // 默认值
            double lastPm25 = 30.0;

            if (history != null && !history.isEmpty()) {
                // 取最新的一条（List 最后一个是最新，因为我们 SQL 是 ORDER BY record_time ASC）
                AirRecord last = history.get(history.size() - 1);
                lastAqi = last.getAqi();
                lastPm25 = last.getPm25();
            }

            // 3. 模拟波动：AQI 在 -10 到 +10 之间波动
            int change = random.nextInt(21) - 10;
            int newAqi = Math.max(0, Math.min(500, lastAqi + change)); // 限制在 0-500

            // PM2.5 稍微波动
            double newPm25 = Math.max(0, lastPm25 + (random.nextDouble() * 4 - 2));

            // 4. 计算新的等级
            String newLevel = getLevel(newAqi);

            // 5. 存入数据库
            AirRecord newRecord = new AirRecord();
            newRecord.setCityName(city);
            newRecord.setAqi(newAqi);
            newRecord.setPm25(Double.parseDouble(String.format("%.1f", newPm25)));
            newRecord.setLevel(newLevel);
            newRecord.setRecordTime(LocalDateTime.now());

            airMapper.insert(newRecord);
        }

        System.out.println("【数据引擎】新一轮数据已入库，大屏将自动更新。");
    }

    // 辅助方法：计算空气等级
    private String getLevel(int aqi) {
        if (aqi <= 50) return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        if (aqi <= 300) return "重度污染";
        return "严重污染";
    }
}