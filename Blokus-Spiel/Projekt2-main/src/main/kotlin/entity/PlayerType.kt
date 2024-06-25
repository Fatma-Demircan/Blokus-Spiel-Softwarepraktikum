package entity

/**
 * Enum to distinguish between the four player types:
 * human, dummy (controlled by all players), random ai, hard ai.
 */
enum class PlayerType {
    HUMAN,
    DUMMY,
    RANDOM_AI,
    HARD_AI,
    MINI_MAX_AI,
    ALPHA_BETA_AI,
    MONTE_CARLO_AI,
    NET_PLAYER
    ;
}