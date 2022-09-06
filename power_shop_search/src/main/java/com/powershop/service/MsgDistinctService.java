package com.powershop.service;

import com.powershop.pojo.MsgDistinct;

public interface MsgDistinctService {
    MsgDistinct getMsgDistinctByTxNo(String txNo);

    void insertMsgDistinct(MsgDistinct msgDistinct);

}
