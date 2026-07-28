export interface FolderResponse {
  id: string;
  parentId: string | null;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface FileResponse {
  id: string;
  folderId: string | null;
  originalName: string;
  contentType: string;
  sizeBytes: number;
  checksumSha256: string;
  createdAt: string;
  updatedAt: string;
}

export interface FolderCrumb {
  id: string | null;
  name: string;
}
