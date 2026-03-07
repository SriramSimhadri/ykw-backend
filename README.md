# ykw-backend

- YKW is a modern content-sharing platform that allows users to write, publish, and interact with articles. 
- It is designed using a microservices architecture, enabling each service to scale independently and communicate via APIs.
- Core Features
    - User Management (User-Service)
      - Register, authenticate, and manage user profiles.
      - Follow/unfollow other users and maintain follower/following counts.
    - Articles (Article-Service)
       - Users can create, edit, publish, or delete articles (drafts or published).
       - Supports article tags, slugs, cover images, reading time, and article stats.
       - Personalized feeds for users based on authors they follow.
    - Article Likes (Article-Likes Service)
       - Users can like/unlike articles.
       - Track total likes per article and which users liked it.
    - Comments (Article-Comments Service)
       - Users can comment on articles, update/delete their comments, and like/unlike comments.
       - Maintains likes count per comment and provides paginated lists.
    - Follows (Follows Service)
       - Users can follow/unfollow other users.
       - Provides paginated lists of followers and following users.

- Architecture & Design
  - Microservices-based: User, Article, Article-Likes, Article-Comments, and Follows services operate independently, each with its own database.
  - API-first: Services communicate via REST APIs; user and article info is fetched on-demand across services.
  - Eventual consistency: Counts like likes_count, comments_count, and followers_count can be updated asynchronously for scalability.
  - Scalable & Production-ready: Each service has indexes and constraints for performance and data integrity.
  - Security: Authentication and authorization are handled via JWT tokens, ensuring secure access to protected endpoints.

In short: YKW lets users create, share, discover, and engage with content, while maintaining a social ecosystem with followers, likes, and comments — all built in a modular, scalable, and maintainable backend.

# YKW Architecture (Design IN PROGRESS) looks like below

                                                                    Client
                                                                       │
                                                                       ▼
                                                                  Load Balancer
                                                                        │
                                                            ┌───────────┼───────────┐
                                                            ▼           ▼           ▼
                                                        API Gateway  API Gateway  API Gateway
                                                            │           │           │
                                                            └───────────┼───────────┘
                                                                        ▼
                                                                Microservices Layer
                                                            ┌──────┬──────┬──────┬──────┐
                                                            ▼      ▼      ▼      ▼      ▼
                                                            Auth   User  Article  Likes  Comments
                                                           Service Service Service Service Service



# user-service requirements:

- The user-service is primarily responsible for user management. 
  - Registration and login
  - Profile management
  - Following/unfollowing users
  - Retrieving user details and lists
  - Updating user statistics (followers/following count)
 It should NOT handle articles/posts - those go to the article-service. Instead, user-service can provide user profile info for other services via APIs.
 
## user-service API endpoints:

| HTTP(S) Method | Endpoint                  | Description                              | Notes                                                                      |
|----------------|---------------------------|------------------------------------------|----------------------------------------------------------------------------|
| POST           | /users/register           | Create a new user account                | Validate email uniqueness, hash password (BCrypt)                          |
| POST           | /users/login              | Authenticate user and return JWT         | Use Spring Security with JWT                                               |
| GET            | /users/{id}               | Get user profile by ID                   | Return public info (name, bio, profile image, followers/following count)   |
| PUT            | /users/{id}               | Update user profile                      | Authenticated user only; allow updating name, bio, profile_image_url       |
| GET            | /users/{id}/followers     | Get list of followers                    | Paginated; return only IDs and names (or full profile if needed)           |
| GET            | /users/{id}/following     | Get list of users this user follows      | Paginated                                                                  |
| POST           | /users/{id}/follow        | Follow a user                            | Authenticated user; increments followers_count/following_count atomically  |
| POST           | /users/{id}/unfollow      | Unfollow a user                          | Authenticated user; decrements counts atomically                           |
| GET            | /users/search             | Search users by name/email               | Support query params: `?q=<keyword>&page=1&size=20`                        |
| GET            | /users/me                 | Get currently authenticated user profile | Use JWT from `Authorization` header                                        |

# article-service requirements:
- Create, update, delete articles (draft/published)
- Get article(s) by slug, author, or tags
- Manage tags associated with articles
- Maintain stats: likes_count, comments_count, reading_time
- Pagination for feeds
- Search and filter articles

## article-service API endpoints:

| HTTP(S) Method | Endpoint                         | Description                                            | Notes                                                                           |
|----------------|----------------------------------|--------------------------------------------------------|---------------------------------------------------------------------------------|
| POST           | /articles                        | Create a new article                                   | Requires authenticated user; can be DRAFT or PUBLISHED                          |
| GET            | /articles/{slug}                 | Get article by slug                                    | Returns article details with author info                                        |
| PUT            | /articles/{slug}                 | Update article                                         | Authenticated author only; update content, title, subtitle, cover image, status |
| DELETE         | /articles/{slug}                 | Delete article                                         | Authenticated author only; cascade deletes article_tags                         |
| GET            | /articles                        | List all published articles                            | Pagination; optional filters: author_id, tag, search query                      |
| GET            | /articles/feed                   | Personalized feed for authenticated user               | Returns articles from followed authors; paginated                               |
| POST           | /articles/{slug}/like            | Like an article                                        | Increment likes_count atomically; auth required                                 |
| POST           | /articles/{slug}/unlike          | Unlike an article                                      | Decrement likes_count atomically; auth required                                 |
| GET            | /articles/{slug}/tags            | Get tags for a specific article                        | Returns array of tag names                                                      |
| POST           | /articles/{slug}/tags            | Add tag(s) to article                                  | Authenticated author only; create tags if not exist                             |
| DELETE         | /articles/{slug}/tags/{tagName}  | Remove tag from article                                | Authenticated author only                                                       |
| GET            | /tags                            | List all tags                                          | For auto-complete or trending tags                                              |
| GET            | /articles/search                 | Search articles by title, content, or tags             | Support `?q=keyword&tag=xyz&page=1&size=20`                                     |

## article-likes-service requirements:
- Track which users liked which articles (article_likes table).
- Maintain counts of likes per article (can be pushed to article-service via API or event).
- Provide APIs for:
  - Liking/unliking an article
  - Fetching whether a user liked an article
  - Fetching total likes for an article
  - Fetching all articles a user liked

## article-likes-service API endpoints

| HTTP(S) Method | Endpoint                                     | Description                                          | Notes                                                             |
|----------------|----------------------------------------------|------------------------------------------------------|-------------------------------------------------------------------|
| POST           | /likes                                       | Like an article                                      | Requires authenticated user; create record in `article_likes`     |
| DELETE         | /likes                                       | Unlike an article                                    | Authenticated user; delete record from `article_likes`            |
| GET            | /likes/article/{articleId}                   | Get total likes for an article                       | Returns count and optionally list of user IDs                     |
| GET            | /likes/user/{userId}                         | Get all articles liked by a user                     | Paginated; use `idx_article_likes_user` for fast lookup           |
| GET            | /likes/article/{articleId}/user/{userId}     | Check if a specific user liked a specific article    | Returns true/false                                                |


## article-comments-service requirements:
- The service handles all comment-related operations:
  - Create, update, delete comments for articles.
  - Fetch comments for a given article (with pagination).
  - Fetch comments made by a user.
  - Like/unlike a comment.
  - Maintain likes_count for each comment.
- Integration with article-service (update comments_count) and user-service (user info for each comment).

## article-comments-service API endpoints

| HTTP Method | Endpoint                            | Description                                          | Notes                                                              |
|-------------|-------------------------------------|------------------------------------------------------|--------------------------------------------------------------------|
| POST        | /comments                           | Add a comment to an article                          | Requires authenticated user; provide `article_id` and `content`    |
| PUT         | /comments/{id}                      | Update a comment                                     | Authenticated user only; update `content`                          |
| DELETE      | /comments/{id}                      | Delete a comment                                     | Authenticated user only                                            |
| GET         | /comments/article/{articleId}       | Get all comments for an article                      | Paginated; sorted by `created_at`                                  |
| GET         | /comments/user/{userId}             | Get all comments made by a user                      | Paginated; use `idx_article_comments_user` for fast lookup         |
| POST        | /comments/{id}/like                 | Like a comment                                       | Increment `likes_count`; auth required                             |
| POST        | /comments/{id}/unlike               | Unlike a comment                                     | Decrement `likes_count`; auth required                             |
| GET         | /comments/{id}/likes                | Get total likes for a comment                        | Returns count and optionally list of users who liked               |


## follows-service requirements:
- The service handles all operations related to following/unfollowing users:
  - Follow/unfollow another user.
  - Get a list of users a user is following.
  - Get a list of followers of a user.
  - Enforce business rules: e.g., users cannot follow themselves (CHECK (follower_id <> following_id)).
  - Support pagination for follower/following lists.
  - Provide counts for followers/following if needed by other services.

## follows-service API endpoints:

| HTTP(S) Method | Endpoint                             | Description                                           | Notes                                                              |
|----------------|--------------------------------------|-------------------------------------------------------|--------------------------------------------------------------------|
| POST           | /follows/{userId}                    | Follow a user                                         | Authenticated user follows `userId`; cannot follow self            |
| DELETE         | /follows/{userId}                    | Unfollow a user                                       | Authenticated user unfollows `userId`                              |
| GET            | /follows/{userId}/followers          | Get all followers of a user                           | Paginated; use `idx_follows_following` for fast lookup             |
| GET            | /follows/{userId}/following          | Get all users that the user is following              | Paginated                                                          |
| GET            | /follows/{followerId}/{followingId}  | Check if a user follows another user                  | Returns true/false                                                 |