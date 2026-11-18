package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 地址簿
 */
@Data
@Schema(description = "地址簿")
public class AddressBook implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;


    //用户id
    @Schema(description = "用户id")
    private Long userId;


    //收货人
    @Schema(description = "收货人")
    private String consignee;


    //手机号
    @Schema(description = "手机号")
    private String phone;


    //性别 0 女 1 男
    @Schema(description = "性别")
    private String sex;


    //省级区划编号
    @Schema(description = "省级区划编号")
    private String provinceCode;


    //省级名称
    @Schema(description = "省级名称")
    private String provinceName;


    //市级区划编号
    @Schema(description = "市级区划编号")
    private String cityCode;


    //市级名称
    @Schema(description = "市级名称")
    private String cityName;


    //区级区划编号
    @Schema(description = "区级区划编号")
    private String districtCode;


    //区级名称
    @Schema(description = "区级名称")
    private String districtName;


    //详细地址
    @Schema(description = "详细地址")
    private String detail;


    //标签
    @Schema(description = "标签")
    private String label;

    //是否默认 0 否 1是
    @Schema(description = "是否默认")
    private Integer isDefault;

    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    //创建人
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    //修改人
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;


    //是否删除
    private Integer isDeleted;
}
