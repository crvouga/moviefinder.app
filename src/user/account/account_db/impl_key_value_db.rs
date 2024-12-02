use std::sync::Arc;

use async_trait::async_trait;

use crate::{key_value_db::interface::KeyValueDb, user::account::account_::Account};

use super::interface::AccountDb;

pub struct ImplKeyValueDb {
    account_by_user_id: Box<dyn KeyValueDb>,
    user_id_by_phone_number: Box<dyn KeyValueDb>,
}

impl ImplKeyValueDb {
    pub fn new(key_value_db: Arc<dyn KeyValueDb>) -> Self {
        Self {
            account_by_user_id: key_value_db
                .clone()
                .child(vec!["user".to_string(), "account".to_string()]),

            user_id_by_phone_number: key_value_db
                .child(vec!["user".to_string(), "user_id".to_string()]),
        }
    }
}

#[async_trait]
impl AccountDb for ImplKeyValueDb {
    async fn find_one_by_phone_number(
        &self,
        phone_number: &str,
    ) -> Result<Option<Account>, std::io::Error> {
        let maybe_user_id = self.user_id_by_phone_number.get(phone_number).await?;

        let user_id = match maybe_user_id {
            Some(user_id) => user_id,
            None => return Ok(None),
        };

        let maybe_account = self.account_by_user_id.get(&user_id).await?;

        let account = match maybe_account {
            Some(account) => account,
            None => return Ok(None),
        };

        let parsed = serde_json::from_str::<Account>(&account)
            .map_err(|err| std::io::Error::new(std::io::ErrorKind::InvalidData, err.to_string()))?;

        Ok(Some(parsed))
    }

    async fn upsert_one(&self, account: &Account) -> Result<(), std::io::Error> {
        let user_id = account.user_id.as_str().to_string();
        let phone_number = account.phone_number.clone();

        let serialized = serde_json::to_string(account)
            .map_err(|err| std::io::Error::new(std::io::ErrorKind::InvalidData, err.to_string()))?;

        self.account_by_user_id
            .put(&user_id, serialized.to_string())
            .await?;

        self.user_id_by_phone_number
            .put(&phone_number, user_id.to_string())
            .await?;

        Ok(())
    }
}