import AppConfig from '@/config';
import type { LoginRequest, SignupRequest, JwtResponse, UserResponse } from '@/dto/auth.dto';
import ApiHelper from '@/helpers/api_helper';

export default class AuthApi {
  public static async login(data: LoginRequest): Promise<JwtResponse> {
    const response = await ApiHelper.post(AppConfig.apiLoginUrl, undefined, data);
    return response.data as JwtResponse;
  }

  public static async signup(data: SignupRequest): Promise<UserResponse> {
    const response = await ApiHelper.post(AppConfig.apiRegisterUrl, undefined, data);
    return response.data as UserResponse;
  }

  public static async getCurrentUser(): Promise<UserResponse> {
    const response = await ApiHelper.get(AppConfig.apiProfileUrl);
    return response.data as UserResponse;
  }
}
