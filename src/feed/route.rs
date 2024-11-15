use serde::{Deserialize, Serialize};

use super::{controls, feed_id::FeedId};

#[derive(Serialize, Deserialize, Debug, PartialEq, Clone)]
pub enum Route {
    DefaultLoad,

    Default,

    IndexLoad {
        feed_id: FeedId,
    },

    Index {
        feed_id: FeedId,
    },

    LoadMore {
        feed_id: FeedId,
        start_feed_index: usize,
    },

    ChangedSlide {
        feed_id: FeedId,
    },

    Controls {
        feed_id: FeedId,
        child: controls::route::Route,
    },
}
