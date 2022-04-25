package com.askii.video.service.video;

import io.humble.video.Codec;
import io.humble.video.Encoder;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class VideoRecorderService {

    private Encoder encoder = null;
    private MediaPictureConverter converter = null;
    private MediaPacket packet = null;
    private MediaPicture picture = null;
    private Muxer muxer = null;

    public void endRecord() {
        do {
            encoder.encode(packet, null);
            if (packet.isComplete())
                muxer.write(packet,  false);
        } while (packet.isComplete());

        muxer.close();
    }

    public void addImage(BufferedImage screen, int index) {

        if (converter == null) {
            converter = MediaPictureConverterFactory.createConverter(screen, picture);
        }
        converter.toPicture(picture, screen, index);

        do {
            encoder.encode(packet, picture);
            if (packet.isComplete())
                muxer.write(packet, false);
        } while (packet.isComplete());
    }

    public void initRecorder(String filename, String formatname, int snapsPerSecond) throws IOException, InterruptedException {
        muxer = Muxer.make(filename, null, formatname);
        final Rational framerate = Rational.make(1, snapsPerSecond);

        /** Now, we need to decide what type of codec to use to encode video. Muxers
         * have limited sets of codecs they can use. We're going to pick the first one that
         * works, or if the user supplied a codec name, we're going to force-fit that
         * in instead.
         */
        final MuxerFormat format = muxer.getFormat();
        final Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());


        /**
         * Now that we know what codec, we need to create an encoder
         */
        encoder = Encoder.make(codec);

        /**
         * Video encoders need to know at a minimum:
         *   width
         *   height
         *   pixel format
         * Some also need to know frame-rate (older codecs that had a fixed rate at which video files could
         * be written needed this). There are many other options you can set on an encoder, but we're
         * going to keep it simpler here.
         */
        encoder.setWidth(1700);
        encoder.setHeight(1140);
        // We are going to use 420P as the format because that's what most video formats these days use
        final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
        encoder.setPixelFormat(pixelformat);
        encoder.setTimeBase(framerate);

        /** An annoynace of some formats is that they need global (rather than per-stream) headers,
         * and in that case you have to tell the encoder. And since Encoders are decoupled from
         * Muxers, there is no easy way to know this beyond
         */
        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
            encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);

        /** Open the encoder. */
        encoder.open(null, null);


        /** Add this stream to the muxer. */
        muxer.addNewStream(encoder);

        /** And open the muxer for business. */
        muxer.open(null, null);

        /** Next, we need to make sure we have the right MediaPicture format objects
         * to encode data with. Java (and most on-screen graphics programs) use some
         * variant of Red-Green-Blue image encoding (a.k.a. RGB or BGR). Most video
         * codecs use some variant of YCrCb formatting. So we're going to have to
         * convert. To do that, we'll introduce a MediaPictureConverter object later. object.
         */
        picture = MediaPicture.make(
                encoder.getWidth(),
                encoder.getHeight(),
                pixelformat);
        picture.setTimeBase(framerate);

        /** Now begin our main loop of taking screen snaps.
         * We're going to encode and then write out any resulting packets. */
        packet = MediaPacket.make();
    }

    /**
     * Convert a {@link BufferedImage} of any type, to {@link BufferedImage} of a
     * specified type. If the source image is the same type as the target type,
     * then original image is returned, otherwise new image of the correct type is
     * created and the content of the source image is copied into the new image.
     *
     * @param sourceImage
     *          the image to be converted
     * @param targetType
     *          the desired BufferedImage type
     *
     * @return a BufferedImage of the specifed target type.
     *
     * @see BufferedImage
     */

    public static BufferedImage convertToType(BufferedImage sourceImage,
                                              int targetType)
    {
        BufferedImage image;

        // if the source image is already the target type, return the source image

        if (sourceImage.getType() == targetType)
            image = sourceImage;

            // otherwise create a new image of the target type and draw the new
            // image

        else
        {
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;
    }

}
