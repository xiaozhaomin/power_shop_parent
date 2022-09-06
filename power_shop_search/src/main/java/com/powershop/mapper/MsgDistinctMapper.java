package com.powershop.mapper;

import com.powershop.pojo.MsgDistinct;
import com.powershop.pojo.MsgDistinctExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MsgDistinctMapper {
    int countByExample(MsgDistinctExample example);

    int deleteByExample(MsgDistinctExample example);

    int deleteByPrimaryKey(String txNo);

    int insert(MsgDistinct record);

    int insertSelective(MsgDistinct record);

    List<MsgDistinct> selectByExample(MsgDistinctExample example);

    MsgDistinct selectByPrimaryKey(String txNo);

    int updateByExampleSelective(@Param("record") MsgDistinct record, @Param("example") MsgDistinctExample example);

    int updateByExample(@Param("record") MsgDistinct record, @Param("example") MsgDistinctExample example);

    int updateByPrimaryKeySelective(MsgDistinct record);

    int updateByPrimaryKey(MsgDistinct record);
}