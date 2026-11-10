// A user shown in a list (droppers, likers...)
export type UserBrief = {
    id: number;
    username: string;
    display_name: string | null;
    avatar_color: string;
    avatar_style: string;
};