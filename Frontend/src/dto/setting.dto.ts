export interface SettingRequest {
  settingType: string;
  name: string;
  providerKey?: string;
  icon?: string;
  description?: string;
  active: boolean;
}

export interface SettingResponse {
  id: number;
  settingType: string;
  name: string;
  providerKey?: string;
  icon?: string;
  description?: string;
  active: boolean;
}
