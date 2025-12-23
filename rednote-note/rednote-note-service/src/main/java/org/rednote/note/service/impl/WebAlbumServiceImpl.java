package org.rednote.note.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.rednote.note.api.entity.WebAlbum;
import org.rednote.note.mapper.WebAlbumMapper;
import org.rednote.note.service.IWebAlbumService;
import org.springframework.stereotype.Service;

@Service
public class WebAlbumServiceImpl extends ServiceImpl<WebAlbumMapper, WebAlbum> implements IWebAlbumService {
}
