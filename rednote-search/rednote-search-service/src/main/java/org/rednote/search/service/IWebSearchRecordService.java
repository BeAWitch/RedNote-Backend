package org.rednote.search.service;

import org.rednote.search.api.vo.RecordSearchVO;

import java.util.List;

/**
 * 搜索记录
 */
public interface IWebSearchRecordService {

    /**
     * 获取搜索记录
     */
    List<RecordSearchVO> getRecordByKeyWord(String keyword);

    /**
     * 热门关键词
     */
    List<RecordSearchVO> getHotRecord(int count);

    /**
     * 增加搜索记录
     */
    void addRecord(String keyword);

    /**
     * 清空搜索记录
     */
    void clearAllRecord();
}
