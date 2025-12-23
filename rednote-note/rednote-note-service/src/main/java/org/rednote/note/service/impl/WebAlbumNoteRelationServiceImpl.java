package org.rednote.note.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.rednote.note.api.entity.WebAlbumNoteRelation;
import org.rednote.note.mapper.WebAlbumNoteRelationMapper;
import org.rednote.note.service.IWebAlbumNoteRelationService;
import org.springframework.stereotype.Service;

@Service
public class WebAlbumNoteRelationServiceImpl extends ServiceImpl<WebAlbumNoteRelationMapper, WebAlbumNoteRelation> implements IWebAlbumNoteRelationService {
}
