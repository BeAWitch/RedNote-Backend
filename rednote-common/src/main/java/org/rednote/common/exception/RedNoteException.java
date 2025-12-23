package org.rednote.common.exception;

import lombok.Data;
import org.rednote.common.enums.ResultCodeEnum;

@Data
public class RedNoteException extends RuntimeException{

    // 异常状态码
    private Integer code;


    /**
     * 通过状态码和错误消息创建异常对象
     *
     * @param message
     * @param code
     */
    public RedNoteException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public RedNoteException(String message) {
        super(message);
        this.code = ResultCodeEnum.FAIL.getCode();
    }

    /**
     * 接收枚举类型对象
     *
     * @param resultCodeEnum
     */
    public RedNoteException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

}
