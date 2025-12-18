package org.rednote.utils;

import com.alibaba.fastjson2.JSON;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.rednote.domain.dto.WSMessageDTO;

public class WSMessageEncoder implements Encoder.Text<WSMessageDTO> {

    @Override
    public String encode(WSMessageDTO wsMessageDTO) throws EncodeException {
        return JSON.toJSONString(wsMessageDTO);
    }

}
