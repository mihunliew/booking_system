import axios, { AxiosError } from "axios";
import { getStoredToken, removeStoredAuth } from "./auth.helper";

export default class ApiHelper {
  // Common headers configuration
  public static getHeaders(): any {
    const headers: any = {
      "Content-Type": "application/json",
    };
    const token = getStoredToken();
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
  }

  // Handle unauthorized responses
  public static checkResponseCode(status: number | undefined) {
    if (status === 401 || status === 403) {
      if (window.location.pathname !== "/login") {
        removeStoredAuth();
        window.location.href = "/login";
      }
    }
  }

  // Convert object to FormData
  public static async convertToFormData(body: object): Promise<FormData> {
    const formData = new FormData();
    Object.entries(body).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        formData.append(key, value instanceof Blob ? value : String(value));
      }
    });
    return formData;
  }

  // Get API Response
  public static async get(
    url: string,
    headers?: any,
    contentType: string = "application/json",
    messageKey: string = "message",
    dataKey?: string,
    statusKey: string = "error",
    statusValueDefaultFalse: any = false
  ): Promise<any> {
    headers = headers ?? ApiHelper.getHeaders() ?? {};
    try {
      headers["Content-Type"] = contentType;
      const response = await axios.get(url, { headers });

      // Handle raw backend responses vs wrapped responses
      let data = response.data;
      if (dataKey && response.data && response.data[dataKey] !== undefined) {
        data = response.data[dataKey];
      }

      let message = "";
      if (response.data && response.data[messageKey] !== undefined) {
        message = response.data[messageKey].toString();
      }

      let successful = true;
      if (statusKey && response.data && response.data[statusKey] !== undefined) {
        successful = response.data[statusKey] === statusValueDefaultFalse;
      }

      if (successful) {
        return { message, data: data || response.data }; // Return data directly if no wrapper
      } else {
        throw message;
      }
    } catch (e) {
      ApiHelper.handleError(e, url);
    }
  }

  // Post API Response
  public static async post(
    url: string,
    headers?: any,
    body?: object,
    contentType: string = "application/json",
    messageKey: string = "message",
    dataKey?: string,
    statusKey: string = "error",
    statusValueDefaultFalse: any = false
  ): Promise<any> {
    headers = headers ?? ApiHelper.getHeaders() ?? {};
    try {
      headers["Content-Type"] = contentType;
      let response: any;

      if (contentType === "multipart/form-data") {
        const formData = body ? await ApiHelper.convertToFormData(body) : new FormData();
        response = await axios.post(url, formData, { headers });
      } else {
        response = await axios.post(url, body, { headers });
      }

      let data = response.data;
      if (dataKey && response.data && response.data[dataKey] !== undefined) {
        data = response.data[dataKey];
      }

      let message = "";
      if (response.data && response.data[messageKey] !== undefined) {
        message = response.data[messageKey].toString();
      }

      let successful = true;
      if (statusKey && response.data && response.data[statusKey] !== undefined) {
        successful = response.data[statusKey] === statusValueDefaultFalse;
      }

      if (successful) {
        return { message, data: data || response.data };
      } else {
        throw message;
      }
    } catch (e) {
      ApiHelper.handleError(e, url);
    }
  }

  // Put API Response
  public static async put(
    url: string,
    headers?: any,
    body?: object,
    contentType: string = "application/json",
    messageKey: string = "message",
    dataKey?: string,
    statusKey: string = "error",
    statusValueDefaultFalse: any = false
  ): Promise<any> {
    headers = headers ?? ApiHelper.getHeaders() ?? {};
    try {
      headers["Content-Type"] = contentType;
      let response: any;

      if (contentType === "multipart/form-data") {
        const formData = body ? await ApiHelper.convertToFormData(body) : new FormData();
        response = await axios.put(url, formData, { headers });
      } else {
        response = await axios.put(url, body, { headers });
      }

      let data = response.data;
      if (dataKey && response.data && response.data[dataKey] !== undefined) {
        data = response.data[dataKey];
      }

      let message = "";
      if (response.data && response.data[messageKey] !== undefined) {
        message = response.data[messageKey].toString();
      }

      let successful = true;
      if (statusKey && response.data && response.data[statusKey] !== undefined) {
        successful = response.data[statusKey] === statusValueDefaultFalse;
      }

      if (successful) {
        return { message, data: data || response.data };
      } else {
        throw message;
      }
    } catch (e) {
      ApiHelper.handleError(e, url);
    }
  }

  // Delete API Response
  public static async delete(
    url: string,
    headers?: any,
    contentType: string = "application/json",
    messageKey: string = "message",
    dataKey?: string,
    statusKey: string = "error",
    statusValueDefaultFalse: any = false
  ): Promise<any> {
    headers = headers ?? ApiHelper.getHeaders() ?? {};
    try {
      headers["Content-Type"] = contentType;
      const response = await axios.delete(url, { headers });

      let data = response.data;
      if (dataKey && response.data && response.data[dataKey] !== undefined) {
        data = response.data[dataKey];
      }

      let message = "";
      if (response.data && response.data[messageKey] !== undefined) {
        message = response.data[messageKey].toString();
      }

      let successful = true;
      if (statusKey && response.data && response.data[statusKey] !== undefined) {
        successful = response.data[statusKey] === statusValueDefaultFalse;
      }

      if (successful) {
        return { message, data: data || response.data };
      } else {
        throw message;
      }
    } catch (e) {
      ApiHelper.handleError(e, url);
    }
  }

  // Common Error Handler
  private static handleError(e: any, url: string) {
    if (e instanceof AxiosError) {
      ApiHelper.checkResponseCode(e.response?.status);

      if (e.response?.status !== 401) {
        if (e.response?.data?.errors) {
          if (typeof e.response.data.errors === 'object') {
            const errorVals = Object.values(e.response.data.errors);
            if (Array.isArray(errorVals) && errorVals.length > 0) {
              const firstVal = errorVals[0];
              if (Array.isArray(firstVal)) {
                throw firstVal.join(', ');
              } else if (typeof firstVal === 'string') {
                throw errorVals.join(', ');
              }
            }
          }
        }
        if (e.response?.data?.message) {
          throw e.response.data.message;
        }
        throw e.message || 'An unexpected error occurred';
      }
    } else if (e instanceof Error) {
      throw e.message || e.toString();
    } else if (typeof e === 'string') {
      throw e;
    }
    throw e;
  }
}
