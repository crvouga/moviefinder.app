import { z } from "zod";
import { firestore, functions } from "../firebase-client";
import { collection, doc, getDoc } from "firebase/firestore";
import { Err, Ok, type Result } from "../result";
import { httpsCallable } from "firebase/functions";

//
//
//
//
//
//
//

export const MovieVideoItemStatus = z.enum(["uploading", "done", "error"]);

export const MovieVideoItem = z.object({
  tmdbMovieId: z.number(),
  youtubeVideoKey: z.string(),
  status: MovieVideoItemStatus,
});

export type MovieVideoItem = z.infer<typeof MovieVideoItem>;

export const toId = ({
  tmdbMovieId,
  youtubeVideoKey,
}: MovieVideoItem): string => {
  return `tmdbMovieId:${tmdbMovieId}-youtubeVideoKey:${youtubeVideoKey}`;
};

//
//
//
//
//
//

const movieVideoItemsCol = collection(firestore, "movie-video-items");

export const getMovieVideoItem = async ({
  tmdbId,
  youtubeVideoKey,
}: {
  tmdbId: number;
  youtubeVideoKey: string;
}): Promise<Result<string, MovieVideoItem | null>> => {
  const id = toId({ tmdbMovieId: tmdbId, youtubeVideoKey: youtubeVideoKey });

  const docRef = doc(movieVideoItemsCol, id);

  const snapshot = await getDoc(docRef);

  if (!snapshot.exists()) {
    return Ok(null);
  }

  const docData = snapshot.data();

  const parsed = MovieVideoItem.safeParse(docData);

  if (!parsed.success) {
    return Err(`Failed to parse`);
  }

  return Ok(parsed.data);
};

const downloadMovieVideoItem = httpsCallable(
  functions,
  "downloadMovieVideoItem"
);

export const uploadMovieVideoItem = async ({
  youTubeVideoKey,
  tmdbMovieId,
}: {
  youTubeVideoKey: string;
  tmdbMovieId: number;
}) => {
  downloadMovieVideoItem({
    youTubeVideoKey,
    tmdbMovieId,
  });
  return;
};
