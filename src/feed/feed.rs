use super::feed_id::FeedId;

#[derive(Debug, Clone, PartialEq)]
pub struct Feed {
    pub feed_id: FeedId,
    pub active_index: usize,
}

impl Feed {
    pub fn random() -> Self {
        Self {
            feed_id: FeedId::new("feed_id".to_string()),
            active_index: 0,
        }
    }
}