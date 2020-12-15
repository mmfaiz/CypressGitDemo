package com.matchi

import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import org.apache.commons.io.FilenameUtils

/**
 * @author Sergei Shushkevich
 */
class ImageService {

    static transactional = false

    def grailsApplication

    /**
     * Creates web optimized version of image.
     *
     * @param imageUrl URL of the original image
     *
     * @return optimized image
     */
    File createOptimizedImage(URL imageUrl) {
        def img = File.createTempFile("tmp-", ".jpg")
        Thumbnails.of(imageUrl)
                .scale(1.0)
                .outputFormat("jpg")
                .toFile(img)
        img
    }

    /**
     * Creates image thumbnail.
     *
     * @param imageUrl URL of the original image
     * @param x the horizontal-component of the top left-hand corner of the area to crop
     * @param y the vertical-component of the top left-hand corner of the area to crop
     * @param width width of the area to crop
     * @param height height of the area to crop
     *
     * @return thumbnail
     */
    File createThumbnail(URL imageUrl, Integer x = null, Integer y = null,
            Integer width = null, Integer height = null) {
        def ext = FilenameUtils.getExtension(imageUrl.toString())
        def thumb = File.createTempFile("tmp-", ".$ext")

        def builder = Thumbnails.of(imageUrl)
                .crop(Positions.CENTER)
                .size(grailsApplication.config.images.thumb.square.size, grailsApplication.config.images.thumb.square.size)
        if (x != null && y != null && width != null && height != null) {
            builder.sourceRegion(x, y, width, height)
        }
        builder.toFile(thumb)

        thumb
    }
}
