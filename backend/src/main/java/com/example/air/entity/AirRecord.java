package com.example.air.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("air_record")
public class AirRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String cityName;
    private Integer aqi;
    private Double pm25;
    private String level;
    private LocalDateTime recordTime;
}
