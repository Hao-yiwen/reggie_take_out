package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时存储菜品和分类信息
     * @param setmealDto
     */
    public void savaWithDish(SetmealDto setmealDto);
    public void deleteWithDish(List<Long> ids);
}
