module [Media]

import ImageSet
import MediaId
import MediaType
import MediaVideo

Media : {
    mediaId : MediaId.MediaId,
    mediaTitle : Str,
    mediaDescription : Str,
    mediaType : MediaType.MediaType,
    mediaPoster : ImageSet.ImageSet,
    mediaBackdrop : ImageSet.ImageSet,
    mediaVideos : List MediaVideo.MediaVideo,
}

