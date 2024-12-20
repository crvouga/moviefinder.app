use serde::{Deserialize, Serialize};

use crate::{
    ctx::Ctx,
    feed::{feed_::Feed, feed_id::FeedId, feed_tag::FeedTag},
};

#[derive(Default, Serialize, Deserialize, Debug, Clone, Eq, PartialEq)]
pub struct FormState {
    pub feed_id: FeedId,
    pub tags: Vec<FeedTag>,
}

impl FormState {
    pub fn new(feed: &Feed) -> Self {
        Self {
            feed_id: feed.feed_id.clone(),
            tags: feed.tags.clone(),
        }
    }

    pub async fn load(ctx: &Ctx, feed: &Feed) -> Self {
        let feed_id = feed.feed_id.clone();

        let maybe_form_state = ctx
            .feed_tags_form_state_db
            .get(&feed_id)
            .await
            .unwrap_or(None);

        let mut form_state = maybe_form_state.unwrap_or(Self::new(feed));
        form_state.feed_id = feed_id;

        form_state
    }
}
