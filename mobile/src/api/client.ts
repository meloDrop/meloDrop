// Every call to the meloDrop API goes through apiFetch (like RetrofitClient + ApiService)

const API_URL = process.env.EXPO_PUBLIC_API_URL;

// An error sent back by the API: the HTTP code and the "detail" field of the JSON
export class ApiError extends Error {
    status: number;
    detail: string;

    constructor(status: number, detail: string) {
        super(detail);
        this.status = status;
        this.detail = detail;
    }
}

// <T>: the type of the JSON we expect, like the return type of a Retrofit method
export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
    let response: Response;
    try {
        response = await fetch(`${API_URL}${path}`, {
            ...options,
            headers: { 'Content-Type': 'application/json' },
        });
    } catch {
        // No answer at all: no network, or wrong address in .env
        throw new ApiError(0, 'network_error');
    }

    // 4xx or 5xx: read the "detail" field sent by FastAPI
    if (!response.ok) {
        let detail = 'unknown_error';
        try {
            const body = await response.json();
            if (typeof body.detail === 'string') detail = body.detail;
        } catch {
            // No JSON in the answer: keep "unknown_error"
        }
        throw new ApiError(response.status, detail);
    }

    // 204 No Content: nothing to read
    if (response.status === 204) return undefined as T;

    return (await response.json()) as T;
}