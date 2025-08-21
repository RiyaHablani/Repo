package com.hashedin.huspark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.util.DataMaskingUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String name;
    private String email;
    private Role role;
    
    // Custom getter for masked email
    @JsonProperty("email")
    public String getMaskedEmail() {
        return DataMaskingUtil.maskEmail(this.email);
    }
    
    // Custom getter for masked name
    @JsonProperty("name")
    public String getMaskedName() {
        return DataMaskingUtil.maskName(this.name);
    }
    
    // Internal getter for unmasked email (for internal use only)
    public String getUnmaskedEmail() {
        return this.email;
    }
    
    // Internal getter for unmasked name (for internal use only)
    public String getUnmaskedName() {
        return this.name;
    }
}
