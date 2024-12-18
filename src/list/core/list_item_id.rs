use serde::{Deserialize, Serialize};

use crate::core::uuid;

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ListItemId(String);

impl ListItemId {
    pub fn from_string(id: &str) -> Self {
        Self(id.to_string())
    }

    // pub fn as_str(&self) -> &str {
    //     &self.0
    // }
}

impl Default for ListItemId {
    fn default() -> Self {
        ListItemId::from_string(&format!("list-item-id-{}", uuid::v4()))
    }
}

impl From<String> for ListItemId {
    fn from(value: String) -> Self {
        Self::from_string(&value)
    }
}
