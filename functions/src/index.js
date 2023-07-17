const logger = require("firebase-functions/logger");
const { onCall } = require("firebase-functions/v2/https");
const ytdl = require("ytdl-core");
const admin = require("firebase-admin");
const fs = require("fs");

// const storage = admin.storage();
// const bucket = storage.bucket("movie-video-item");

module.exports.uploadMovieVideoItem = onCall(async (request) => {
  try {
    const { tmdbMovieId, youTubeVideoKey } = request.data;

    const videoUrl = `https://www.youtube.com/watch?v=${youTubeVideoKey}`;

    logger.info(`tmdbMovieId: ${tmdbMovieId}`);
    logger.info(`youTubeVideoKey: ${youTubeVideoKey}`);
    logger.info(`videoUrl: ${videoUrl}`);

    logger.info(`getInfo`);
    const info = await ytdl.getInfo(youTubeVideoKey);

    const quality = "lowest";

    const format = ytdl.chooseFormat(info.formats, {
      quality: quality,
      filter(format) {
        return format.hasVideo && format.hasAudio && format.container === "mp4";
      },
    });

    const videoStream = ytdl(videoUrl, { format });

    videoStream.on("end", () => {
      logger.info("video download ended");
    });

    videoStream.on("error", () => {
      logger.error("video download error");
    });

    videoStream.on("progress", (_chunkLength, downloaded, total) => {
      const percent = downloaded / total;
      const formattedPercent = `%${(percent * 100).toFixed(2)}`;
      logger.info(`downloaded: ${formattedPercent}`);
    });

    const videoPath = `${__dirname}/tmp/${youTubeVideoKey}.mp4`;
    logger.info(`videoPath: ${videoPath}`);

    const fileStream = fs.createWriteStream(videoPath);
    logger.info(`saving to file system`);

    await new Promise((resolve, reject) => {
      videoStream.pipe(fileStream);
      videoStream.on("error", reject);
      fileStream.on("finish", resolve);
    });
    // logger.info("upload time");
    // await bucketName.upload(videoPath, { destination: fileName });

    return {
      tmdbMovieId,
      youTubeVideoKey,
      videoUrl,
      info,
      videoPath,
      format,
    };
  } catch (error) {
    logger.error(error);
    return {
      error: String(error),
    };
  }
});
