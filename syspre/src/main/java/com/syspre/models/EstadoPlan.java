package com.syspre.models;

public enum EstadoPlan {
    SIN_PLAN,             // 0. Sin plan creado
    PLAN_CREADO,          // 1. Plan creado (pendiente evaluación docente)
    EVALUADO_DOCENTE,     // 2. Evaluado por docente (pendiente aprobación coordinador)
    APROBADO_COORDINADOR, // 3. Aprobado por coordinador (esperando fecha inicio)
    EN_EJECUCION,         // 4. Prácticas en ejecución (según fecha_inicio)
    REPORTES_ACTIVOS,     // 5. Puede crear reportes semanales y trabajo en empresa
    INFORME_FINAL,        // 6. Puede crear informe final (cuando termine el período)
    SUSTENTACION          // 7. Esperando fecha de sustentación (informe aprobado)ado
}