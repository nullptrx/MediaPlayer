package com.alivc.player;

import android.content.Context;
import com.aliyun.aliyunplayer.R;

public enum AliyunErrorCode {
    ALIVC_ERR_INVALID_PARAM(4001, R.string.alivc_err_invalid_param),
    ALIVC_ERR_AUTH_EXPIRED(4002, R.string.alivc_err_auth_expried),
    ALIVC_ERR_INVALID_INPUTFILE(4003, R.string.alivc_err_invalid_inutfile),
    ALIVC_ERROR_NO_INPUTFILE(4004, R.string.alivc_err_no_inputfile),
    ALIVC_ERR_READ_DATA_FAILED(4005, R.string.alivc_err_read_data_failed),
    ALIVC_ERR_READ_METADATA_FAILED(4006, R.string.alivc_err_read_metadata_failed),
    ALIVC_ERR_PLAY_FAILED(4007, R.string.alivc_err_play_failed),
    ALIVC_ERROR_LOADING_TIMEOUT(4008, R.string.alivc_err_loading_timeout),
    ALIVC_ERROR_REQUEST_DATA_ERROR(4009, R.string.alivc_err_request_data_error),
    ALIVC_ERROR_ENCRYPTED_VIDEO_UNSUPORTED(4010, R.string.alivc_err_vencrypted_video_unsuported),
    ALIVC_ERROR_VIDEO_FORMAT_UNSUPORTED(4011, R.string.alivc_err_video_format_unsupported),
    ALIVC_ERROR_PLAYAUTH_PARSE_FAILED(4012, R.string.alivc_err_playauth_parse_failed),
    ALIVC_ERROR_DECODE_FAILED(4013, R.string.alivc_err_decode_failed),
    ALIVC_ERR_NO_NETWORK(4014, R.string.alivc_err_no_network),
    ALIVC_ERR_MEDIA_ABORTED(4015, R.string.alivc_err_media_abort),
    ALIVC_ERR_LOADING_FAILED(4016, R.string.alivc_err_loading_failed),
    ALIVC_ERR_MEDIA_UNSUPPORTED(4018, R.string.alivc_err_media_unsopproted),
    ALIVC_ERR_NO_SUPPORT_CODEC(4019, R.string.alivc_err_no_support_codec),
    ALIVC_ERR_ILLEGALSTATUS(4021, R.string.alivc_err_illegalstatus),
    ALIVC_ERR_NO_VIEW(4022, R.string.alivc_err_no_view),
    ALIVC_ERR_NO_MEMORY(4023, R.string.alivc_err_no_memory),
    ALIVC_ERR_FUNCTION_DENIED(4024, R.string.alivc_err_function_denied),
    ALIVC_ERR_UNKNOWN(4400, R.string.alivc_err_unkown),
    ALIVC_ERR_NO_STORAGE_PERMISSION(4401, R.string.alivc_err_no_storage_permission),
    ALIVC_ERR_REQUEST_ERROR(4500, R.string.alivc_err_request_error),
    ALIVC_ERR_DATA_ERROR(4501, R.string.alivc_err_data_error),
    ALIVC_ERR_QEQUEST_SAAS_SERVER_ERROR(4502, R.string.alivc_err_request_saas_server_error),
    ALIVC_ERR_QEQUEST_MTS_SERVER_ERROR(4503, R.string.alivc_err_request_mts_server_error),
    ALIVC_ERR__SERVER_INVALID_PARAM(4504, R.string.alivc_err_server_invalid_param),
    ALIVC_ERR_DOWNLOAD_NO_NETWORK(4101, R.string.alivc_err_download_no_network),
    ALIVC_ERR_DOWNLOAD_NETWORK_TIMEOUT(4102, R.string.alivc_err_download_network_timeout),
    ALIVC_ERR_DOWNLOAD_QEQUEST_SAAS_SERVER_ERROR(4103, R.string.alivc_err_download_request_saas_server_error),
    ALIVC_ERR_DOWNLOAD_QEQUEST_MTS_SERVER_ERROR(4104, R.string.alivc_err_download_request_mts_serveer_error),
    ALIVC_ERR_DOWNLOAD_SERVER_INVALID_PARAM(4105, R.string.alivc_err_download_server_invalid_param),
    ALIVC_ERR_DOWNLOAD_INVALID_INPUTFILE(4106, R.string.alivc_err_download_invalid_inputfile),
    ALIVC_ERR_DOWNLOAD_NO_ENCRYPT_FILE(4107, R.string.alivc_err_download_no_encrypt_file),
    ALIVC_ERR_DONWNLOAD_GET_KEY(4108, R.string.alivc_err_download_get_key),
    ALIVC_ERR_DOWNLOAD_INVALID_URL(4109, R.string.alivc_err_download_invalid_url),
    ALIVC_ERR_DONWLOAD_NO_SPACE(4110, R.string.alivc_err_download_no_space),
    ALIVC_ERR_DOWNLOAD_INVALID_SAVE_PATH(4111, R.string.aliv_err_download_invalid_save_path),
    ALIVC_ERR_DOWNLOAD_NO_PERMISSION(4112, R.string.alivc_err_download_no_permission),
    ALIVC_ERR_DOWNLOAD_MODE_CHANGED(4113, R.string.alivc_download_mode_changed),
    ALIVC_ERR_DOWNLOAD_ALREADY_ADDED(4114, R.string.alivc_err_download_already_added),
    ALIVC_ERR_DOWNLOAD_NO_MATCH(4115, R.string.alivc_err_download_no_match),
    ALIVC_SUCCESS(0, R.string.alivc_success);
    
    private final int code;
    private final int descriptionId;

    private AliyunErrorCode(int code, int descriptionId) {
        this.code = code;
        this.descriptionId = descriptionId;
    }

    public String getDescription(Context context) {
        return context.getString(this.descriptionId);
    }

    public int getCode() {
        return this.code;
    }

    public static AliyunErrorCode getErrorCode(int code) {
        for (AliyunErrorCode aliyunErrorCode : values()) {
            if (aliyunErrorCode.getCode() == code) {
                return aliyunErrorCode;
            }
        }
        return ALIVC_SUCCESS;
    }
}
