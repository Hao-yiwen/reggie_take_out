# 问题

```agsl
DishDto dishDto = new DishDto();
BeanUtils.copyProperties(item, dishDto);
Long categroyId = item.getCategoryId();
Category category = categoryService.getById(categroyId);
String categoryName = category.getName();
dishDto.setCategoryName(categoryName);
```
上述代码为什么不用LambdaQueryWrapper 直接通过getById获取了值 不是需要LambdaQueryWrapper获取吗
