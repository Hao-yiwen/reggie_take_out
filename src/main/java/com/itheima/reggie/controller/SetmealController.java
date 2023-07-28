package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PostMapping
    @ApiOperation("新增套餐接口")
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息: {}", setmealDto);
        setmealService.savaWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("新增分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",readOnly = true),
            @ApiImplicitParam(name = "pageSize",value = "没业记录数",readOnly = true),
            @ApiImplicitParam(name = "name",value = "套餐名称",readOnly = false)
    })
    public R<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageinfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageinfo, queryWrapper);
        BeanUtils.copyProperties(pageinfo, setmealDtoPage, "records");
        setmealDtoPage.setRecords(pageinfo.getRecords().stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            String categroyName = categoryService.getById(item.getCategoryId()).getName();
            if (categroyName != null) {
                setmealDto.setCategoryName(categroyName);
            }
            return setmealDto;
        }).collect(Collectors.toList()));
        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐/批量删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info(ids.toString());
//      判断状态是否是停售
        setmealService.deleteWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * C端查询套餐内容
     *
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value="setmealCache",key="#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<SetmealDto>> list(Setmeal setmeal) {
        log.info(setmeal.toString());
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        List<SetmealDto> setmealDtoList = setmealList.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            LambdaQueryWrapper<SetmealDish> setmealDtoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDtoLambdaQueryWrapper.eq(SetmealDish::getSetmealId, item.getId());
            setmealDto.setSetmealDishes(setmealDishService.list(setmealDtoLambdaQueryWrapper));
            return setmealDto;
        }).collect(Collectors.toList());
        return R.success(setmealDtoList);
    }
}
