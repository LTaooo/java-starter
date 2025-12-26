package com.lt.springstarter.base;

import org.springframework.lang.Nullable;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public abstract class BaseRepository<T_MAPPER extends BaseMapper<T_MODEL>, T_MODEL> {
    protected final T_MAPPER mapper;

    protected BaseRepository(T_MAPPER mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据ID查询实体
     *
     * @param id 实体ID
     * @return 实体对象
     */
    public @Nullable T_MODEL getById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 根据ID列表查询实体列表
     *
     * @param ids 实体ID列表
     * @return 实体对象列表
     */
    public List<T_MODEL> getListByIds(List<Long> ids) {
        return mapper.selectByIds(ids);
    }

    /**
     * 插入实体
     *
     * @param model 实体对象
     * @return 插入成功的记录数
     */
    public int insert(T_MODEL model) {
        return mapper.insert(model);
    }
}
