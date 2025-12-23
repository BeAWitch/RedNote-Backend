package org.rednote.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.note.api.entity.WebNote;
import org.rednote.note.api.entity.WebUserNoteRelation;
import org.rednote.note.api.vo.NoteVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 笔记管理
 */
public interface IWebNoteService extends IService<WebNote> {

    /**
     * 获取笔记
     *
     * @param noteId 笔记ID
     */
    NoteVO getNoteById(Long noteId);

    /**
     * 新增笔记
     *
     * @param noteData 笔记对象
     * @param files    文件
     */
    Long saveNoteByDTO(String noteData, MultipartFile[] files);

    /**
     * 删除笔记
     *
     * @param noteIds 笔记ID集合
     */
    void deleteNoteByIds(List<String> noteIds);

    /**
     * 更新笔记
     *
     * @param noteData 笔记对象
     * @param files    图片文件
     */
    void updateNoteByDTO(String noteData, MultipartFile[] files);

    /**
     * 置顶笔记
     *
     * @param noteId 笔记ID
     */
    boolean pinnedNote(Long noteId);

    /**
     * 获取用户笔记关系
     *
     * @param userId 用户 ID
     */
    List<WebUserNoteRelation> getUserNoteRelationByUserIds(Long userId);

    /**
     * 获取笔记列表，按时间降序
     *
     * @param noteIds 笔记 ID 集合
     */
    List<WebNote> getByIdsOrderedByTime(List<Long> noteIds);
}
