const logger = require("firebase-functions/logger");
const { onCall } = require("firebase-functions/v2/https");
const ytdl = require("ytdl-core");
const fs = require("fs");
const admin = require("firebase-admin");

admin.initializeApp();

const storage = admin.storage();
const bucket = storage.bucket();

module.exports.downloadMovieVideoItem = onCall(
  {
    cors: true,
  },
  async (request) => {
    try {
      //
      //
      // Input
      //
      //

      const { tmdbMovieId, youTubeVideoKey } = request.data;

      const videoUrl = `https://www.youtube.com/watch?v=${youTubeVideoKey}`;

      logger.info(`tmdbMovieId: ${tmdbMovieId}`);
      logger.info(`youTubeVideoKey: ${youTubeVideoKey}`);
      logger.info(`videoUrl: ${videoUrl}`);

      //
      //
      // Download from youtube
      //
      //

      logger.info(`getInfo`);

      const info = await ytdl.getInfo(youTubeVideoKey);

      const quality = "lowest";

      const format = ytdl.chooseFormat(info.formats, {
        quality: quality,
        filter(format) {
          return (
            format.hasVideo && format.hasAudio && format.container === "mp4"
          );
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

      //
      //
      // Write to file system
      //
      //

      const videoPath = `${__dirname}/tmp/${youTubeVideoKey}.mp4`;
      logger.info(`videoPath: ${videoPath}`);

      const fileStream = fs.createWriteStream(videoPath);
      logger.info(`saving to file system`);

      await new Promise((resolve, reject) => {
        videoStream.pipe(fileStream);
        videoStream.on("error", reject);
        fileStream.on("finish", resolve);
      });

      //
      //
      //
      // Upload to cloud storage
      //
      //

      logger.info("upload time");
      const fileName = `movie-video-item/${youTubeVideoKey}.mp4`;
      logger.info(`fileName: ${fileName}`);
      await bucket.upload(videoPath, { destination: fileName });

      return {
        fileName,
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
  }
);
