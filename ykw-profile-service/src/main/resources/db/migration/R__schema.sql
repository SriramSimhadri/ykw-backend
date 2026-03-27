DROP TABLE IF EXISTS user_profiles;

CREATE TABLE user_profiles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    bio TEXT,
    profile_image_url TEXT,
    followers_count INTEGER NOT NULL DEFAULT 0,
    following_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_profiles_name ON user_profiles(name);