export type TipoIngrediente = 'SECO' | 'LIQUIDO' | 'REFRIGERADO'
export type Operacao = 'ENTRADA' | 'SAIDA'
export type StatusMovimentacao = 'SUCESSO' | 'FALHA'

export type Ingrediente = {
  id: number
  nome: string
  tipo: TipoIngrediente
  quantidade: number
  compartimentoId: number
  compartimentoCodigo: string
}

export type VolumePorTipo = {
  type: TipoIngrediente
  totalQuantity: number
}

export type CompartimentoDisponivel = {
  id: number
  codigo: string
  tipoAtual: TipoIngrediente | null
  saldo: number
  capacidade: number
  espacoLivre: number
}

export type Movimentacao = {
  id: number
  dataHora: string
  operacao: Operacao
  status: StatusMovimentacao
  quantidade: number
  tipo: TipoIngrediente | null
  responsavel: string
  compartimentoId: number | null
  compartimentoCodigo: string | null
  ingredienteId: number | null
  ingredienteNome: string | null
  metadata: string | null
}

export type FiltroHistorico = {
  operacao: '' | Operacao
  status: '' | StatusMovimentacao
  order: 'asc' | 'desc'
}

export class ApiError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'ApiError'
  }
}

type EntradaPayload = {
  nome: string
  tipo: TipoIngrediente
  quantidade: number
  compartimentoId: number
  responsavel: string
}

type SaidaPayload = {
  quantidade: number
  responsavel: string
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(path, {
      ...init,
      headers: {
        Accept: 'application/json',
        ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
        ...init?.headers,
      },
    })
  } catch {
    throw new ApiError('Não foi possível conectar à API. Verifique se o backend está na porta 8080.')
  }

  if (!response.ok) {
    let message = 'Não foi possível concluir a operação.'
    try {
      const body = (await response.json()) as { message?: string }
      if (body.message) {
        message = body.message
      }
    } catch {
      // a resposta de erro nem sempre vem em JSON
    }
    throw new ApiError(message)
  }

  return (await response.json()) as T
}

export function listarIngredientes() {
  return request<Ingrediente[]>('/ingredientes')
}

export function listarVolume() {
  return request<VolumePorTipo[]>('/ingredientes/volume')
}

export function criarEntrada(payload: EntradaPayload) {
  return request<Ingrediente>('/ingredientes', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function registrarSaida(id: number, payload: SaidaPayload) {
  return request<Ingrediente>(`/ingredientes/${id}/saida`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function listarDisponiveis(quantidade: number, tipo: TipoIngrediente) {
  const params = new URLSearchParams({
    quantidade: String(quantidade),
    tipo,
  })
  return request<CompartimentoDisponivel[]>(`/compartimentos/disponiveis?${params.toString()}`)
}

export function listarHistorico(filtro: FiltroHistorico) {
  const params = new URLSearchParams({
    sortBy: 'data',
    order: filtro.order,
  })
  if (filtro.operacao) {
    params.set('operacao', filtro.operacao)
  }
  if (filtro.status) {
    params.set('status', filtro.status)
  }
  return request<Movimentacao[]>(`/historico?${params.toString()}`)
}
