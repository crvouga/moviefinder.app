use std::sync::Arc;

use async_trait::async_trait;

use crate::{
    key_value_db::interface::KeyValueDb,
    user::{profile::profile_::UserProfile, user_id::UserId},
};

use super::interface::UserProfileDb;

pub struct ImplKeyValueDb {
    profile_by_user_id: Box<dyn KeyValueDb>,
    user_id_by_username: Box<dyn KeyValueDb>,
}

impl ImplKeyValueDb {
    pub fn new(key_value_db: Arc<dyn KeyValueDb>) -> Self {
        Self {
            profile_by_user_id: key_value_db
                .clone()
                .child(vec!["profile_by_user_id".to_string()]),

            user_id_by_username: key_value_db.child(vec!["user_id_by_username".to_string()]),
        }
    }
}

#[async_trait]
impl UserProfileDb for ImplKeyValueDb {
    async fn find_one_by_user_id(
        &self,
        user_id: &UserId,
    ) -> Result<Option<UserProfile>, std::io::Error> {
        let maybe_user_id = self.user_id_by_username.get(user_id.as_str()).await?;

        let user_id = match maybe_user_id {
            Some(user_id) => user_id,
            None => return Ok(None),
        };

        let maybe_profile = self.profile_by_user_id.get(&user_id).await?;

        let profile = match maybe_profile {
            Some(profile) => profile,
            None => return Ok(None),
        };

        let parsed = serde_json::from_str::<UserProfile>(&profile)
            .map_err(|err| std::io::Error::new(std::io::ErrorKind::InvalidData, err.to_string()))?;

        Ok(Some(parsed))
    }

    async fn upsert_one(&self, profile: &UserProfile) -> Result<(), std::io::Error> {
        let user_id = profile.user_id.as_str().to_string();
        let username = profile.username.clone();

        let serialized = serde_json::to_string(profile)
            .map_err(|err| std::io::Error::new(std::io::ErrorKind::InvalidData, err.to_string()))?;

        self.profile_by_user_id
            .put(&user_id, serialized.to_string())
            .await?;

        self.user_id_by_username
            .put(&username, user_id.to_string())
            .await?;

        Ok(())
    }
}