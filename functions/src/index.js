const logger = require("firebase-functions/logger");
const { onCall } = require("firebase-functions/v2/https");

module.exports.uploadMovieVideoItem = onCall(async (request) => {
  const { tmdbMovieId, youTubeVideoKey } = request.data;

  logger.info("Hello logs!", { structuredData: true });
  return {
    tmdbMovieId,
    youTubeVideoKey,
  };
});
