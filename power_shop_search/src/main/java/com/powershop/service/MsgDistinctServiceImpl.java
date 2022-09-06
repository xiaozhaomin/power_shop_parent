package com.powershop.service;

import com.powershop.mapper.MsgDistinctMapper;
import com.powershop.pojo.MsgDistinct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MsgDistinctServiceImpl implements MsgDistinctService {
    @Autowired
    private MsgDistinctMapper msgDistinctMapper;
    @Override
    public MsgDistinct getMsgDistinctByTxNo(String txNo) {
        return msgDistinctMapper.selectByPrimaryKey(txNo);
    }

    @Override
    public void insertMsgDistinct(MsgDistinct msgDistinct) {
        msgDistinctMapper.insert(msgDistinct);

    }
}
