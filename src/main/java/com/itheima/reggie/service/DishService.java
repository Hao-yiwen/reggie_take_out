package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //    新增菜品，同时插入菜品对应的口味数据，需要操作两张表
    public void saveWithFlavor(DishDto dishDto);

    //    根据id查询菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id);

    //    更新菜品，同时插入菜品对应的口味数据，需要操作两张表
    public void updateWithFlavor(DishDto dishDto);
}
