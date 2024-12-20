use super::form_state::FormState;
use crate::{
    core::{
        key_value_db::interface::KeyValueDb, logger::interface::Logger, unit_of_work::UnitOfWork,
    },
    debug,
    feed::feed_id::FeedId,
};
use std::sync::Arc;

mod interface_test;

pub struct FeedTagsFormStateDb {
    key_value_db: Box<dyn KeyValueDb>,
    logger: Arc<dyn Logger>,
}

impl FeedTagsFormStateDb {
    pub fn new(logger: Arc<dyn Logger>, key_value_db: Arc<dyn KeyValueDb>) -> Self {
        Self {
            logger: logger.child("form_state_db"),
            key_value_db: key_value_db.child(vec![
                "feed".to_string(),
                "controls".to_string(),
                "form-state".to_string(),
            ]),
        }
    }

    pub async fn get(&self, feed_id: &FeedId) -> Result<Option<FormState>, std::io::Error> {
        debug!(self.logger, "get {:?}", feed_id);
        let got = self.key_value_db.get(feed_id.as_str()).await.unwrap();

        if got.is_none() {
            return Ok(None);
        }

        let parsed = serde_json::from_str(&got.unwrap())
            .map_err(|e| e.to_string())
            .unwrap();

        Ok(Some(parsed))
    }

    pub async fn put(&self, uow: UnitOfWork, form_state: &FormState) -> Result<(), std::io::Error> {
        debug!(self.logger, "put {:?}", form_state);
        let value = serde_json::to_string(&form_state)
            .map_err(|e| std::io::Error::new(std::io::ErrorKind::InvalidData, e.to_string()))?;

        self.key_value_db
            .put(uow, form_state.feed_id.as_str(), value)
            .await
    }
}
