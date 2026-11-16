package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐
 */
@Data
@Schema(description = "套餐")
public class Setmeal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;


    //分类id
    @Schema(description = "分类id")
    private Long categoryId;


    //套餐名称
    @Schema(description = "套餐名称")
    private String name;


    //套餐价格
    @Schema(description = "套餐价格")
    private BigDecimal price;


    //状态 0:停用 1:启用
    @Schema(description = "状态")
    private Integer status;


    //编码
    @Schema(description = "编码")
    private String code;


    //描述信息
    @Schema(description = "描述信息")
    private String description;


    //图片
    @Schema(description = "图片")
    private String image;


    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;


    //是否删除
    private Integer isDeleted;
}
