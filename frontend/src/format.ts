import type { TipoIngrediente } from '@/api'

export const TIPOS: { id: TipoIngrediente; nome: string; unidade: string; capacidade: number }[] = [
  { id: 'SECO', nome: 'Secos', unidade: 'kg', capacidade: 600 },
  { id: 'LIQUIDO', nome: 'Líquidos', unidade: 'L', capacidade: 500 },
  { id: 'REFRIGERADO', nome: 'Refrigerados', unidade: 'kg', capacidade: 400 },
]

export const COMPARTIMENTOS = [
  { id: 1, codigo: 'C1' },
  { id: 2, codigo: 'C2' },
  { id: 3, codigo: 'C3' },
  { id: 4, codigo: 'C4' },
  { id: 5, codigo: 'C5' },
]

export function tipoInfo(tipo: TipoIngrediente | null) {
  return TIPOS.find((item) => item.id === tipo) ?? null
}

export function nomeTipo(tipo: TipoIngrediente | null) {
  return tipoInfo(tipo)?.nome ?? 'Sem tipo'
}

export function formatarQuantidade(valor: number, tipo: TipoIngrediente | null) {
  const numero = new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: 3,
    maximumFractionDigits: 3,
  }).format(valor)
  const unidade = tipoInfo(tipo)?.unidade
  return unidade ? `${numero} ${unidade}` : numero
}

export function formatarDataHora(valor: string) {
  const data = new Date(valor)
  if (Number.isNaN(data.getTime())) {
    return valor
  }
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(data)
}

export function lerQuantidade(valor: string) {
  const normalizado = valor.trim().replace(',', '.')
  if (!normalizado) {
    return null
  }
  const numero = Number(normalizado)
  if (!Number.isFinite(numero) || numero <= 0) {
    return null
  }
  return numero
}
