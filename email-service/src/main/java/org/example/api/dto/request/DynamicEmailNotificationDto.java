package org.example.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicEmailNotificationDto extends AbstractNontifyDto {
    HashMap<String, Object> variables;
}
