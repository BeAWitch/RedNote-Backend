package org.rednote.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.note.domain.entity.WebNote;
import org.rednote.note.domain.vo.NoteVO;
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
    NoteVO getNoteById(String noteId);

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
    boolean pinnedNote(String noteId);
}
