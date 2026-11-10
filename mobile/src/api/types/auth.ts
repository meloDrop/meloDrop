// Shapes of the JSON sent and received by the meloDrop API (one file per module, like AuthDto.kt).
// Field names stay in snake_case, exactly like in the JSON:
// no @SerializedName needed, the JSON is read as it is.

// POST /auth/login: what the app sends
export type LoginBody = {
    identifier: string; // email or username
    password: string;
};

// POST /auth/login: what the backend sends back
export type Token = {
    access_token: string;
    token_type: string;
};

// GET /me
export type Me = {
    id: number;
    email: string;
    username: string;
    created_at: string;
    pending_email: string | null;
};