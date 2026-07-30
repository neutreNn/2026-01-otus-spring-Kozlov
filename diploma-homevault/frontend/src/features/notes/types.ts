export interface NoteResponse {
  id: string;
  title: string;
  content: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface NoteFormPayload {
  title: string;
  content: string;
  tags: string[];
}
