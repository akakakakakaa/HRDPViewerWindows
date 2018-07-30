//
// Created by yj on 16. 7. 31.
//

#include "codec_Codec.h"

AVCodec *codec = NULL;
AVCodecContext *c = NULL;
AVFrame* frame = NULL;
AVFormatContext *m_pFormatContext = NULL;
AVCodecContext* file_codec = NULL;
AVStream* out_stream = NULL;
int videoWidth;
int videoHeight;

JNIEXPORT void JNICALL Java_codec_Codec_decode_1init
  (JNIEnv *env, jobject jobj, jint width, jint height) {
    av_register_all();
    codec = avcodec_find_decoder(AV_CODEC_ID_H264);
    if (!codec) {
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Codec not found\n");
        exit(1);
    }

    c = avcodec_alloc_context3(codec);
    if (!c) {
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Codec not allocate video codec context\n");
        exit(1);
    }

    //if(codec->capabilities&CODEC_CAP_TRUNCATED)
    //    codec->flags |= CODEC_FLAG_TRUNCATED;

    if (avcodec_open2(c, codec, NULL) < 0) {
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Codec not open codec\n");
        exit(1);
    }
    videoWidth = width;
    videoHeight = height;
  }

JNIEXPORT jbyteArray JNICALL Java_codec_Codec_decode_1frame
  (JNIEnv *env, jobject jobj, jbyteArray data) {
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "decoding start\n");
    jboolean isCopy;
    jbyte* rawBytes = (*env)->GetByteArrayElements(env, data, &isCopy);

    int got_output;
    AVPacket pkt;
    av_init_packet(&pkt);
    pkt.data = rawBytes;
    pkt.size = (*env)->GetArrayLength(env, data);
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "pkt size: pkt size is %d\n", pkt.size);

    frame = av_frame_alloc();
    int len = avcodec_decode_video2(c, frame, &got_output, &pkt);
    if(len < 0) {
        (*env)->ReleaseByteArrayElements(env, data, rawBytes, 0);
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "error occurred : %d \n", len);
        av_free_packet(&pkt);
        av_frame_free(&frame);
        return (*env)->NewByteArray(env, 1);
    }

    if(got_output) {
        jbyteArray result = (*env)->NewByteArray(env, videoWidth*videoHeight*3/2);
        (*env)->SetByteArrayRegion(env, result, 0, frame->linesize[0]*videoHeight, (jbyte*)frame->data[0]);
        (*env)->SetByteArrayRegion(env, result, frame->linesize[0]*videoHeight, frame->linesize[2]*videoHeight/2, (jbyte*)frame->data[1]);
        (*env)->SetByteArrayRegion(env, result, frame->linesize[0]*videoHeight + frame->linesize[2]*videoHeight/2, frame->linesize[1]*videoHeight/2, (jbyte*)frame->data[2]);

        (*env)->ReleaseByteArrayElements(env, data, rawBytes, 0);
        av_free_packet(&pkt);
        av_frame_free(&frame);
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "free free, decoding packet is not null\n");
        return result;
    }
    else {
        (*env)->ReleaseByteArrayElements(env, data, rawBytes, 0);
        av_free_packet(&pkt);
        av_frame_free(&frame);
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "free free, decoding packet is null\n");
        return (*env)->NewByteArray(env, 0);
    }
  }


JNIEXPORT void JNICALL Java_codec_Codec_decode_1release
  (JNIEnv *env, jobject jobj) {
    if(frame != NULL) {
        av_frame_free(&frame);
        frame = NULL;
    }
    if(c != NULL) {
        avcodec_close(c);
        av_free(c);
        c = NULL;
    }
  }

JNIEXPORT void JNICALL Java_codec_Codec_open_1file
  (JNIEnv *env, jobject jobj, jstring filename, jint width, jint height, jint framerate, jint keyframerate, jint pixfmt) {
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "open file start\n");
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "open file start\n");

    int err;
    const char *fPath = (*env)->GetStringUTFChars(env, filename, 0);
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "get file name\n");

    err = avformat_alloc_output_context2(&m_pFormatContext, NULL, NULL, fPath);
    if (!m_pFormatContext || err < 0) {
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "avformat_alloc_output_context2: error %d\n", err);
        exit(1);
    }

    out_stream = avformat_new_stream(m_pFormatContext, NULL);
    file_codec = out_stream->codec;
    file_codec->codec_id = AV_CODEC_ID_H264;
    file_codec->codec_type = AVMEDIA_TYPE_VIDEO;
    file_codec->width = width;
    file_codec->height = height;
    out_stream->time_base.den = framerate;
    out_stream->time_base.num = 1;
    file_codec->time_base.den = framerate;
    file_codec->time_base.num = 1;
    file_codec->gop_size = keyframerate;
    file_codec->pix_fmt = pixfmt;

    //if(m_pFormatContext->oformat->flags & AVFMT_GLOBALHEADER)
    //    file_codec->flags |= CODEC_FLAG_GLOBAL_HEADER;

    err = avio_open(&m_pFormatContext->pb, fPath, AVIO_FLAG_WRITE);
    if(err < 0) {
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "avio_open: error %d\n", err);
        exit(1);
    }

    err = avformat_write_header(m_pFormatContext, NULL);
    if(err < 0) {
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "avformat_write_header: error %d\n", err);
        exit(1);
    }

    (*env)->ReleaseStringUTFChars(env, filename, fPath);
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "open file end\n");

  }

JNIEXPORT void JNICALL Java_codec_Codec_write_1video
  (JNIEnv *env, jobject jobj, jbyteArray data) {
    int err;
    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "write_video start\n");
    jboolean isCopy;
    jbyte* rawBytes = (*env)->GetByteArrayElements(env, data, &isCopy);

    int got_output;
    AVPacket pkt;
    av_init_packet(&pkt);
    pkt.data = rawBytes;
    pkt.size = (*env)->GetArrayLength(env, data);

    err = av_interleaved_write_frame(m_pFormatContext, &pkt);
    if(err < 0) {
        //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "av_interleaved_write_frame: error %d\n", err);
        exit(1);
    }
  }

JNIEXPORT void JNICALL Java_codec_Codec_close_1file
  (JNIEnv *env, jobject jobj) {
    if(m_pFormatContext) {
        av_write_trailer(m_pFormatContext);
        avio_close(m_pFormatContext->pb);
        avformat_free_context(m_pFormatContext);
        m_pFormatContext = NULL;
    }
  }
