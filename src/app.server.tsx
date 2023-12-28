
import { feedRouter } from './@child/feed/feed.server';
import {Server}from './@shared/server'

export const appRouter = ({s}: {s: Server}) => {
  return {
    feed: feedRouter({s})
  }
}

