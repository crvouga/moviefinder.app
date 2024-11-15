use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, Serialize, PartialEq, Deserialize)]
pub enum Route {
    LoadIndex,
    Index,
    ClickedSave,
}
