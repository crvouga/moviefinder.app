
import { authRouter } from './@child/user-auth/auth.server';
import { feedRouter } from './@child/feed/feed.server';
import { Server } from './@shared/server'
import { mediaRouter } from './@child/media/media.server';

export const appRouter = ({ s }: { s: Server }) => {
  return {
    feed: feedRouter({ s }),
    media: mediaRouter({ s }),
    auth: authRouter({ s })
  }
}

